#! /usr/bin/env python
# Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.

import argparse
import re
import os
import subprocess
import platform
import multiprocessing
import webbrowser

try:
    import click
except Exception as ex:
    print("Failed importing external package 'click', "
          "try running 'pip install click'")
    exit(1)
# . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
# Helper Functions


def msg(message, color=None):
    click.echo(click.style(message, fg=color))


def msg_success(message):
    msg(message, color="green")


def msg_failure(message):
    msg(message, color="red")


def msg_status(message):
    msg(message, color="blue")


def msg_normal(message):
    msg(message)


def exec_cmd(cmd, check=False, env=None, shell=False, cwd=None):
    """
    :param shell: run in shell mode
    :param cwd: working directory for the command
    :param env: environment variables to pass to the process
    :param check: when set to True, will print error message and exit
    :param cmd: Command to execute in the form of a list or string
    :return: rc, stdout, stderr
    """
    if type(cmd) is str:
        cmd = cmd.split()
    cmd_string = ' '.join(cmd)
    process = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
        env=env,
        cwd=cwd
    )
    stdout, stderr = process.communicate()
    rc = process.returncode
    if check is True and rc != 0:
        msg_failure("Failed executing '{}'. rc={}. aborting."
                    .format(cmd_string, rc))
        if stdout is not None and stdout != '':
            msg_failure("STDOUT: {}".format(stdout))
        if stderr is not None and stderr != '':
            msg_failure("STDERR: {}".format(stderr))
        exit(rc)
    return rc, stdout, stderr

# . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
# Global variables
script_path = os.path.realpath(__file__)
script_dir = os.path.dirname(script_path)

# . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

components = {
    'nui': 'nazgul.nui.version',
    'psb': 'nazgul.psb.version',
    'utilities': 'nazgul.utilities.version',
    'site': 'nazgul.site.version',
    'hub': 'nazgul.hub.version',
    'dsb': 'nazgul.dsb.version',
    'mysql-dsb': 'nazgul.mysql-dsb.version',
    'copyrepo': 'nazgul.copyrepo.version',
    'cfpsb': 'nazgul.cfpsb.version',
    'vmware': 'nazgul.vmware.version',
    'microservice-api': 'microservice.api.version',
    'microservice-runtime': 'microservice.runtime.version',
    'naztest': 'nazgul.naztest.version',
    'pcf-tools': 'nazgul.pcf-tools.version',
    'fscrb': 'nazgul.fscrb.version',
}

parser = argparse.ArgumentParser(description='Bump "component"\'s version in '
                                             '"repo" repository, or in all '
                                             'repositories if repo is not '
                                             'specified')
parser.add_argument('--nazgul-top-level', '-n', required=True)
parser.add_argument('--component', '-c', choices=components.keys(),
                    required=True)
parser.add_argument('--version', '-v', type=str, required=True)
parser.add_argument('--commit', '-t', help='commit changes after bumping',
                    action='store_true')
parser.add_argument('--push', '-p',
                    help='push changes after bumping. must be used together '
                         'with --commit',
                    action='store_true')


VERSION_BUMP_BRANCH_NAME = 'automated-version-bump'


def replace_version_string(repo_path, component, version):
    """
    Updates the version in the repo's parent pom.
    """
    component_key = components.get(component)
    pattern = '<{0}>.*</{0}>'.format(component_key)
    replacement = '<{0}>{1}</{0}>'.format(component_key, version)

    pom_path = find_pom_file(repo_path)
    with open(pom_path) as pom:
        lines = pom.readlines()

    with open(pom_path, 'w') as pom:
        for line in lines:
            if component_key in line:
                line = re.sub(pattern, replacement, line)
            pom.write(line)


def find_pom_file(repo_path):
    pom_path = os.path.join(repo_path, 'pom.xml')
    if os.path.exists(pom_path):
        return pom_path


def is_git_repo(dir_path):
    rc, stdout, stderr = exec_cmd('git rev-parse', cwd=dir_path)
    return rc == 0


def is_repo_status_clean(repo_path):
    _, stdout, _ = exec_cmd('git status --porcelain', cwd=repo_path)
    return stdout == ''


def git_commit(repo, component, version):
    """
    Commit the updated pom file
    """
    msg_normal("Committing changes for repo '{}'".format(repo))
    pom_path = find_pom_file(repo)
    exec_cmd('git add {}'.format(pom_path), check=True, cwd=repo)
    exec_cmd(['git', 'commit', '-m',
              'Bumping {} version to {} (Automated version bump)'
             .format(component, version)], check=True, cwd=repo)


def git_push(repo):
    """
    Pushes the version bump branch.
    :return url address that will create an MR for that branch
    """
    msg_normal("Pushing changes for repo '{}'".format(repo))
    _, _, stderr = exec_cmd(
        'git push -f origin {}'.format(VERSION_BUMP_BRANCH_NAME),
        check=True, cwd=repo)

    merge_request_url = [x for x in stderr.splitlines()
                         if 'https' in x][0].split()[1]
    msg_normal("Create an MR using this link: {}".format(merge_request_url))
    return merge_request_url


def git_fetch(repo):
    msg_normal("Fetching master branch for repo '{}'".format(repo))
    exec_cmd('git fetch', check=True, cwd=repo)


def git_pull_master(repo):
    msg_normal("Pulling master branch for repo '{}'".format(repo))
    exec_cmd('git checkout master', cwd=repo)
    exec_cmd('git pull', check=True, cwd=repo)


def repo_needs_to_be_updated(repo, component, version):
    pom_path = find_pom_file(repo)
    if pom_path is None:
        return False

    component_key = components.get(component)
    version_regexp = '<{0}>.*</{0}>'.format(component_key)
    new_version_element = '<{0}>{1}</{0}>'.format(component_key, version)
    _, pom_lines, _ = exec_cmd('git show origin/master:pom.xml',
                               check=True, cwd=repo)
    for line in pom_lines.splitlines():
        if re.search(version_regexp, line):
            if new_version_element not in line:
                return True
    return False


def delete_old_version_bump_branch(repo):
    exec_cmd('git checkout master', cwd=repo)
    exec_cmd('git branch -fD {}'.format(VERSION_BUMP_BRANCH_NAME), cwd=repo)


def create_version_bump_branch(repo):
    exec_cmd('git checkout -b {}'.format(VERSION_BUMP_BRANCH_NAME),
             check=True, cwd=repo)


def create_merge_request(merge_request_url):
    webbrowser.open_new_tab(merge_request_url)


def fetch_all_repos(repos):
    if len(repos) < 1:
        return
    pool = multiprocessing.Pool(8)
    pool.map(git_fetch, repos)


def pull_all_repos(repos):
    if len(repos) < 1:
        return
    pool = multiprocessing.Pool(8)
    pool.map(git_pull_master, repos)


def bump_version(repos_directory, component, version, commit=False, push=False):
    repos_directory = os.path.abspath(os.path.expanduser(repos_directory))
    _, directories, _ = next(os.walk(repos_directory))
    directories = [os.path.join(repos_directory, x) for x in directories]

    repos = [x for x in directories if is_git_repo(x)]

    msg_status("Bumping '{}' version to {}".format(component, version))

    fetch_all_repos(repos)

    repos_to_update = [x for x in repos
                       if repo_needs_to_be_updated(x, component, version)]
    del repos

    unclean_repos = [x for x in repos_to_update
                     if not is_repo_status_clean(x)]
    if len(unclean_repos) != 0:
        msg_failure("The following repos are not in clean state:\n\t" +
                    '\n\t'.join(unclean_repos))
        msg_failure('First make sure all repos are in a clean state, and then '
                    'try again')
        exit(1)
    del unclean_repos

    if len(repos_to_update) != 0:
        msg_status("The following repos will be affected:\n\t{}"
                   .format('\n\t'.join(repos_to_update)))
        if not click.confirm('Are you sure you want to continue?'):
            msg_status("Aborting...")
            exit(0)

    pull_all_repos(repos_to_update)
    for repo in repos_to_update:
        delete_old_version_bump_branch(repo)
        create_version_bump_branch(repo)
        replace_version_string(repo, component, version)

        if commit is True:
            git_commit(repo, component, version)

        if push is True:
            merge_request_url = git_push(repo)
            create_merge_request(merge_request_url)

        msg_success("Successfully bumped '{}' to version {} in repo {}"
                    .format(component, version, repo))

    msg_success("All repos are up to date, exiting")


def validate_args(args):
    if args.push and not args.commit:
        msg_failure('--push must be used together with --commit')
        exit(1)

    if 'SNAPSHOT' in args.version and args.commit is True:
        msg_failure('Cannot commit SNAPSHOT versions')
        exit(1)


def main():
    args = parser.parse_args()
    validate_args(args)
    bump_version(
        args.nazgul_top_level,
        args.component,
        args.version,
        commit=args.commit,
        push=args.push)


if __name__ == '__main__':
    main()
